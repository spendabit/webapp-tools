package co.spendabit.webapp.forms

import javax.servlet.http.HttpServletRequest

import co.spendabit.webapp.forms.controls.LabeledControl

abstract class BaseWebForm[T <: scala.Product] {

  protected val fields: Seq[LabeledControl[_]]

  protected def seqToTuple(s: Seq[_]): T

  protected def widgetsHTML(values: Option[T]): Seq[xml.NodeSeq]

  def html(action: String, method: String, values: Option[T],
           btnLabel: xml.NodeSeq = <span>Submit</span>): xml.NodeSeq = {

//    val fieldsMarkup = fields.map { f =>
//
//      // TODO: Ensure something unique here!
//      val fieldID = f.label.toLowerCase.replace(' ', '-').filter(c => c.isLetterOrDigit || c == '-')
//
//      <div class="form-group">
//        <label for={ fieldID } class="col-sm-3 control-label">{ f.label }</label>
//        <div class="col-sm-9">
//          { f.widgetHTML(fieldID) }
//        </div>
//      </div>
//    }

    val fieldsMarkup: xml.NodeSeq = {
      val labels = fields.map { f =>
        val fieldID = f.label.toLowerCase.replace(' ', '-').
          filter(c => c.isLetterOrDigit || c == '-')
        <label for={ fieldID } class="col-sm-3 control-label">{ f.label }</label>
      }
      // TODO: Add 'id' attributes to fields!
      val widgets = widgetsHTML(values)
      val combined = labels.zip(widgets).map { case (l, w) =>
        <div class="form-group">
          { l } <div class="col-sm-9">{ w }</div>
        </div>
      }
      combined.tail.foldLeft(xml.NodeSeq.fromSeq(combined.head)) {
        case (soFar, e) => soFar ++ e
      }
    }

    /* If the form includes an <input type="file" .../> node, we need to use multipart/form-data. */
    val encoding = {
      val fileInputs = (fieldsMarkup \\ "input").filter {
        _.attribute("type") match {
          case Some(Seq(xml.Text(v))) => v.toLowerCase == "file"
          case None => false
        }
      }
      if (fileInputs.length > 0) "multipart/form-data"
      else "application/x-www-form-urlencoded"
    }

    <form action={ action } method={ method } enctype={ encoding }
          class="form-horizontal" role="form">
      { fieldsMarkup }
      <div class="form-group form-submit">
        <div class="col-sm-offset-3 col-sm-9">
          <button type="submit" class="btn btn-primary">{ btnLabel }</button>
        </div>
      </div>
    </form>
  }

//  def renderField(fieldID: String,) =
//    <div class="form-group">
//      <label for={ fieldID } class="col-sm-3 control-label">{ f.label }</label>
//      <div class="col-sm-9">
//        { f.widgetHTML(fieldID) }
//      </div>
//    </div>

  def validate(request: HttpServletRequest): ValidationResult[T] = {
    import scala.collection.JavaConverters._
    val params = request.getParameterMap.asScala.map(p => (p._1, p._2.toSeq)).toMap
    validate(params)
  }

  def validate(params: Map[String, Seq[String]]): ValidationResult[T] = {

    val validationResults = fields.map(f => f.validate(params))

    if (validationResults.count(_.isRight) == validationResults.length)
      Valid(seqToTuple(validationResults.map(_.right.get)))
    else
      Invalid(validationResults.map(_.left.toSeq).flatten)
  }
}
