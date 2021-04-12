package co.spendabit.webapp.forms.v3.controls

abstract class GenericInput[T] extends TextEntryControl[T] {

  def inputType: String

  def valueAsString(value: T): String

  def html(value: Option[T] = None): xml.NodeSeq =
    <input type={ inputType } value={ value.map(v => valueAsString(v)).getOrElse("") } />
//    <input type={ inputType } class="form-control" name={ name } placeholder={ placeholder }
//           value={ value.map(v => valueAsString(v)).getOrElse("") } />
}
