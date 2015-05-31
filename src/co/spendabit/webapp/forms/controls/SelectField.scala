package co.spendabit.webapp.forms.controls

abstract class SelectField[T](override val label: String, name: String, options: Seq[T])
        extends LabeledControl[T](label) {

  /** Must provide the (unique) value that will be placed in the underlying <option> element's
    * 'value' attribute.
    */
  protected def optionValue(opt: T): String

  protected def optionLabel(opt: T): String

  override def widgetHTML(value: Option[T] = None): xml.NodeSeq =
    <select name={ name } class="form-control">
      { options.map { o => <option value={ optionValue(o) }>{ optionLabel(o) }</option> } }
    </select>

  def validate(params: Map[String, Seq[String]]): Either[String, T] = {
    val valueOfSelected = params(name).headOption.getOrElse("")
    options.find(o => optionValue(o) == valueOfSelected) match {
      case Some(v) => Right(v)
      case None => Left(s"Please select a value for $label.")
    }
  }
}
