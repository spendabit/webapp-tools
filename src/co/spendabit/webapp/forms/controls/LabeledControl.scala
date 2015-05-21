package co.spendabit.webapp.forms.controls

abstract class LabeledControl[T](val label: String/*, val value: Option[T]*/) {
  def validate(params: Map[String, Seq[String]]): Either[String, T]
  def widgetHTML(value: Option[T] = None): xml.NodeSeq
//  def widgetHTML(id: String, value: Option[T] = None): xml.NodeSeq
//  def widgetHTML(id: String): xml.NodeSeq
}
