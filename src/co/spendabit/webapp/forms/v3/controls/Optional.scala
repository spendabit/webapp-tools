package co.spendabit.webapp.forms.v3.controls

case class Optional[T](control: TextEntryControl[T]) extends TextEntryControl[Option[T]] {

  def html(value: Option[Option[T]] = None): xml.NodeSeq =
    control.html(value.flatten)

  override def validate(value: String): Either[String, Option[T]] =
    if (value.trim == "")
      Right(None)
    else
      control.validate(value).right.map(Some(_))

//  def validate(s: String): Either[String, Option[T]] =
//    control.validate(s).right.map(v => Some(v))
}
