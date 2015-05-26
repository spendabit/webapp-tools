package co.spendabit.webapp.forms.controls

/** A control that accepts a value as free-form text (e.g., <input type="text"/>, <textarea/>),
  * though it may validate that the entered text matches a specififc format (such as that of an
  * integer, or URL).
  */
abstract class TextEntryControl[T](override val label: String, val name: String)
        extends LabeledControl[T](label) {

//  def valueAsString(value: String): String = value

  def validate(s: String): Either[String, T]
//  def widgetHTML(id: String, value: Option[String] = None): xml.Node

  def validate(params: Map[String, Seq[String]]): Either[String, T] = {

    params.get(name) match {

      case Some(Seq(v)) =>
        validate(v)

      case Some(vs) if vs.length > 1 =>
        // TODO: We should really be returning a 400 response code
        Left(s"Multiple values for field $label?")

      case _ =>
        // TODO: We should really be returning a 400 response code
        Left(s"No value provided for field $label")
    }
  }
}