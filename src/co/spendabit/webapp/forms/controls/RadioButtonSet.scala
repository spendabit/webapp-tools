package co.spendabit.webapp.forms.controls

class RadioButtonSet(override val label: String, val name: String,
                     options: Seq[(String, String)])
        extends LabeledControl[String](label) {

  def widgetHTML(value: Option[String] = None): xml.NodeSeq =
    <div>
      { options.map(o =>
          <label><input type="radio" name={ name } value={ o._1 } /> { o._2 }</label>)
      }
    </div>

  def validate(params: Map[String, Seq[String]]): Either[String, String] =
    params.getOrElse(name, Seq()) match {
      case Seq()  => Left(s"Please provide a value for $label.")
      case Seq(v) => Right(v)
      case _ =>
        // TODO: Should raise exception that eventually leads to 40x response (402?).
        throw new Exception(s"Multiple values for field $name!")
    }
}
