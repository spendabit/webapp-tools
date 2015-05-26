package co.spendabit.webapp.forms.v2

trait CustomHtml[T] extends BaseWebForm[T] {

  def customHtml: xml.Elem

  override def html = customHtml
}
