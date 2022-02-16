package co.spendabit.webapp.forms.v3

import co.spendabit.webapp.forms.{Invalid, Valid, ValidationResult}
import co.spendabit.webapp.{MultipartFormHandling, UploadConfig}
import co.spendabit.webapp.forms.util.{withAttr, withAttrs}
import co.spendabit.webapp.forms.v3.controls.{FileBasedInput, HiddenInput, TextBasedInput}

import javax.servlet.http.HttpServletRequest
import org.apache.commons.fileupload.FileItem

import scala.language.higherKinds
import scala.xml.NodeSeq

abstract class BaseWebForm[F[_] <: Field[_], T] extends MultipartFormHandling {

  /** Override this, providing an instance of `UploadConfig`, for a form that has
    * file-upload (<input type="file" />) fields.
    */
  protected def uploadConfig: Option[UploadConfig] = None

  protected def fieldsSeq: Seq[F[_]]

  protected def seqToTuple(s: Seq[_]): T

  protected def widgetsHTML(values: Option[T]): Seq[xml.NodeSeq]

  protected def fieldNameGenerator: FieldNameGenerator[F] =
    new DefaultNameGenerator[F]

  type ValidationError = String
  protected def crossFieldValidations: Seq[T => Option[ValidationError]] = Seq()

  def fillFields(formElem: xml.Elem, params: Map[String, Seq[String]]): NodeSeq =
    if (formElem.label != "form")
      throw new IllegalArgumentException("Can only fill fields for a <form> element")
    else
      co.spendabit.webapp.forms.util.populateFormFields(formElem, params)

  def html(action: String, method: String, renderer: FormRenderer[F],
           params: Map[String, Seq[String]]): xml.NodeSeq =
    co.spendabit.webapp.forms.util.populateFormFields(
      html(action = action, method = method, renderer), params)

  def html(action: String, method: String, renderer: FormRenderer[F]): xml.NodeSeq = {
    val fieldsMarkup = {

      val fieldsWithNamesAndWidgets = fieldNameGenerator.withNames(fieldsSeq).
        zip(widgetsHTML(None)).map(f => (f._1._2, f._1._1, f._2))
      val combined: Seq[xml.NodeSeq] =
        fieldsWithNamesAndWidgets.map { case (field, name, controlSansName) =>
          val controlWithName = withAttr(controlSansName.asInstanceOf[xml.Elem], "name", name)
          if (field.control == HiddenInput) {
            controlWithName
          } else {
            // TODO: Add 'id' to control and 'for' to label!
            // TODO: Avoid use of `asInstanceOf`!
            renderer.labeledControl(field, controlWithName)
          }
        }

      combined.tail.foldLeft(xml.NodeSeq.fromSeq(combined.head)) {
        case (soFar, e) => soFar ++ e
      }
    }

    val encoding = decideEncoding(fieldsMarkup)
    withAttrs(renderer.formElem(fieldsMarkup), "action" -> action, "method" -> method,
                                               "enctype" -> encoding)
  }

//  def html(params: Map[String, Seq[String]] = Map()): xml.NodeSeq =
//    co.spendabit.webapp.forms.util.populateFormFields(html, params)

//  def html: xml.NodeSeq = {
//    val fieldsMarkup: xml.NodeSeq = {
//      val labels = fieldsSeq.map { f =>
//        val fieldID = f.label.toLowerCase.replace(' ', '-').
//          filter(c => c.isLetterOrDigit || c == '-')
//        <label for={ fieldID } class="col-sm-3 control-label">{ f.label }</label>
//      }
//      // TODO: Add 'id' attributes to fields!
////      val widgets = widgetsHTML(values)
//      val widgets = widgetsHTML(None) // XXX
//      val combined = labels.zip(widgets).map { case (l, w) =>
//        <div class="form-group">
//          { l } <div class="col-sm-9">{ w }</div>
//        </div>
//      }
//      combined.tail.foldLeft(xml.NodeSeq.fromSeq(combined.head)) {
//        case (soFar, e) => soFar ++ e
//      }
//    }
//
//    val encoding = decideEncoding(fieldsMarkup)
//
//    <form action={ action } method={ method.value } enctype={ encoding }
//          class="form-horizontal" role="form">
//      { fieldsMarkup }
//      <div class="form-group form-submit">
//        <div class="col-sm-offset-3 col-sm-9">
//          <button type="submit" class="btn btn-primary">Submit</button>
//        </div>
//      </div>
//    </form>
//  }

  def validate(request: HttpServletRequest): ValidationResult[T] = {
    import scala.collection.JavaConverters._

    val (params, files) =
      if (isMultipartRequest(request))
        decodeMultipartData(request)
      else
        (request.getParameterMap.asScala.map(p => (p._1, p._2.toSeq)).toMap, Seq())

    validate(params, files)
  }

  def validate(params: Map[String, Seq[String]],
               fileItems: Seq[FileItem] = Seq()): ValidationResult[T] = {

    val validationResults = fieldNameGenerator.withNames(fieldsSeq).map { case (name, f) =>
      f.control match {
        case c: TextBasedInput[T] =>
          c.validate(params.get(name).flatMap(_.headOption).getOrElse(""))
        case c: FileBasedInput[T] =>
          c.validate(fileItems.find(_.getFieldName == name))
      }
    }

    if (validationResults.count(_.isRight) == validationResults.length) {
      val tupledValues = seqToTuple(validationResults.map(_.right.get))
      crossFieldValidations.flatMap(f => f(tupledValues)) match {
        case Seq()  => Valid(tupledValues)
        case errors => Invalid(errors)
      }
    } else {
      Invalid(validationResults.flatMap(_.left.toSeq))
    }
  }

  private def decodeMultipartData(request: HttpServletRequest): (Params, Seq[FileItem]) = {

    val items = uploadConfig.map { conf =>
      readMultipartFormData(request, conf)
    }.getOrElse {
      val ct = request.getContentType.split(';').map(_.trim).head
      log.error(s"Received multipart ($ct) request, but `uploadConfig` " +
        s"is not set; no form values will be decoded")
      Seq()
    }

    (items.filter(_.isFormField).map { p => p.getFieldName -> Seq(p.getString)}.toMap,
      items.filter(!_.isFormField))
  }

  /** If the form includes an <input type="file" .../> node, we need to use multipart/form-data.
    */
  private def decideEncoding(formHTML: xml.NodeSeq): String = {
    val fileInputs = (formHTML \\ "input").filter {
      _.attribute("type") match {
        case Some(Seq(xml.Text(v))) => v.toLowerCase == "file"
        case None => false
      }
    }
    if (fileInputs.length > 0) "multipart/form-data"
    else "application/x-www-form-urlencoded"
  }

  type Params = Map[String, Seq[String]]

  private lazy val log = org.log4s.getLogger
}
