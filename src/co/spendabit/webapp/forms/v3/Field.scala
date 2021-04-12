package co.spendabit.webapp.forms.v3

import co.spendabit.webapp.forms.v3.controls.Control

class Field[T](val label: String, val control: Control[T])

object Field {
  def apply[T](label: String, control: Control[T]) =
    new Field(label, control)
}