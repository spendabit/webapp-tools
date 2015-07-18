package co.spendabit.webapp.forms.controls

case class Optional[T](control: TextEntryControl[T])
        extends TextEntryControl[Option[T]](control.label, control.name) {

  def widgetHTML(value: Option[Option[T]] = None) = control.widgetHTML(value.flatten)

  override def validate(params: Map[String, Seq[String]]): Either[String, Option[T]] =
    params.get(control.name) match {
      case None => Right(None)
      case Some(Seq(v)) =>
        if (v.trim == "")
          Right(None)
        else
          super.validate(params)
    }

  def validate(s: String): Either[String, Option[T]] =
    control.validate(s).right.map(v => Some(v))
}
