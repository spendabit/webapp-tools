package co.spendabit.webapp.forms.controls

abstract class GenericInput[T](override val label: String, override val name: String,
                               placeholder: String = "")
        extends TextEntryControl[T](label, name) {

  def inputType: String

  def valueAsString(value: T): String

  override def widgetHTML(value: Option[T] = None): xml.NodeSeq =
    <input type={ inputType } class="form-control" name={ name } placeholder={ placeholder }
           value={ value.map(v => valueAsString(v)).getOrElse("") } />
}
