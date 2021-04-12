package co.spendabit.webapp.forms.v3.controls

/** A control that accepts a value as free-form text (e.g., <input type="text"/>, <textarea/>),
  * though it may validate that the entered text matches a specific format. For example, it may
  * require the value to be a valid URL, or that the value parses to an integer.
  */
abstract class TextEntryControl[T] extends TextBasedInput[T] {

  def validate(s: String): Either[String, T]

//  def validate(params: Map[String, Seq[String]]): Either[String, T] = {
//
//    params.get(name) match {
//
//      case Some(Seq(v)) =>
//        validate(v)
//
//      case Some(vs) if vs.length > 1 =>
//        // TODO: We should really be returning a 400 response code
//        Left(s"Multiple values for field $label?")
//
//      case _ =>
//        // TODO: We should really be returning a 400 response code
//        Left(s"No value provided for field $label")
//    }
//  }
}
