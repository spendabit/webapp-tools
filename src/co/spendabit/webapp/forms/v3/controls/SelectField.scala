package co.spendabit.webapp.forms.v3.controls

abstract class SelectField[T](options: Seq[T]) extends TextBasedInput[T] {

  /** Must provide the (unique) value that will be placed in the corresponding <option> element's
    * 'value' attribute.
    */
  protected def optionValue(opt: T): String

  /** Must provide the label (text content) for the corresponding <option> element.
    */
  protected def optionLabel(opt: T): String

  override def html(value: Option[T] = None): xml.NodeSeq =
    //<select name={ name } class="form-control">
    <select>
      { options.map { o => <option value={ optionValue(o) }>{ optionLabel(o) }</option> } }
    </select>

  def validate(value: String): Either[String, T] = {
//    val valueOfSelected = params(name).headOption.getOrElse("")
    options.find(o => optionValue(o) == value) match {
      case Some(v) => Right(v)
      case None => Left(s"Please select a value for XXX.")
    }
  }

//  def validate(params: Map[String, Seq[String]]): Either[String, T] = {
//    val valueOfSelected = params(name).headOption.getOrElse("")
//    options.find(o => optionValue(o) == valueOfSelected) match {
//      case Some(v) => Right(v)
//      case None => Left(s"Please select a value for $label.")
//    }
//  }
}
