package co.spendabit.webapp.forms.controls

abstract class SelectField[T](override val label: String, override val name: String,
                              options: Seq[(String, String)]/*, selected: Option[String]*/)
        extends GenericInput[T](label, name) {

  def widgetHTML(id: String/*, value: Option[String]*/) =
    <select id={ id } name={ name } class="form-control">
      { options.map { case (optValue, optLabel) =>
//      if (value.orNull == optValue)
//        <option value={ optValue } selected="selected">{ optLabel }</option>
//      else
        <option value={ optValue }>{ optLabel }</option> } }
    </select>
}
