package co.spendabit.webapp.forms.v2

import javax.servlet.http.HttpServletRequest

import co.spendabit.webapp.forms.controls.LabeledControl
import co.spendabit.webapp.forms.ui.FormRenderer
import co.spendabit.webapp.forms.util.withAttrs

abstract class BaseWebForm[T] {

  sealed trait Method { def value: String }
  case object GET  extends Method { def value = "get" }
  case object POST extends Method { def value = "post" }

  protected def method: Method

  protected def action: String

  protected def fieldsSeq: Seq[LabeledControl[_]]

  protected def seqToTuple(s: Seq[_]): T

  protected def widgetsHTML(values: Option[T]): Seq[xml.NodeSeq]

  type ValidationError = String
  protected def crossFieldValidations: Seq[T => Option[ValidationError]] = Seq()

  def html(renderer: FormRenderer, params: Map[String, Seq[String]]): xml.NodeSeq =
    co.spendabit.webapp.forms.util.populateFormFields(html(renderer), params)

  def html(renderer: FormRenderer): xml.NodeSeq = {
    val fieldsMarkup = {

      val widgets = widgetsHTML(None) // XXX
      val combined = fieldsSeq.zip(widgets).map { case (controlObject, controlHTML) =>
        // TODO: Add 'id' to control and 'for' to label!
        renderer.labeledControl(controlObject.label, controlHTML)
      }

      combined.tail.foldLeft(xml.NodeSeq.fromSeq(combined.head)) {
        case (soFar, e) => soFar ++ e
      }
    }

    // TODO: Set encoding!
    withAttrs(renderer.formElem(fieldsMarkup), "action" -> action, "method" -> method.value)
  }

  def html(params: Map[String, Seq[String]] = Map()): xml.NodeSeq =
    co.spendabit.webapp.forms.util.populateFormFields(html, params)

  def html: xml.NodeSeq = {
    val fieldsMarkup: xml.NodeSeq = {
      val labels = fieldsSeq.map { f =>
        val fieldID = f.label.toLowerCase.replace(' ', '-').
          filter(c => c.isLetterOrDigit || c == '-')
        <label for={ fieldID } class="col-sm-3 control-label">{ f.label }</label>
      }
      // TODO: Add 'id' attributes to fields!
//      val widgets = widgetsHTML(values)
      val widgets = widgetsHTML(None) // XXX
      val combined = labels.zip(widgets).map { case (l, w) =>
        <div class="form-group">
          { l } <div class="col-sm-9">{ w }</div>
        </div>
      }
      combined.tail.foldLeft(xml.NodeSeq.fromSeq(combined.head)) {
        case (soFar, e) => soFar ++ e
      }
    }

    val encoding = decideEncoding(fieldsMarkup)

    <form action={ action } method={ method.value } enctype={ encoding }
          class="form-horizontal" role="form">
      { fieldsMarkup }
      <div class="form-group form-submit">
        <div class="col-sm-offset-3 col-sm-9">
          <button type="submit" class="btn btn-primary">Submit</button>
        </div>
      </div>
    </form>
  }

  def validate(request: HttpServletRequest): ValidationResult[T] = {
    import scala.collection.JavaConverters._
    val params = request.getParameterMap.asScala.map(p => (p._1, p._2.toSeq)).toMap
    validate(params)
  }

  def validate(params: Map[String, Seq[String]]): ValidationResult[T] = {

    val validationResults = fieldsSeq.map(f => f.validate(params))

    if (validationResults.count(_.isRight) == validationResults.length) {
      val tupledValues = seqToTuple(validationResults.map(_.right.get))
      crossFieldValidations.map(f => f(tupledValues)).flatten match {
        case Seq()  => Valid(tupledValues)
        case errors => Invalid(errors)
      }
    } else {
      Invalid(validationResults.map(_.left.toSeq).flatten)
    }
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

}
