package co.spendabit.webapp.forms.v3

package object controls {

  sealed abstract class Control[T]/*(val defaultValue: Option[T])*/ {
    def html(value: Option[T] = None): xml.NodeSeq
  }

  abstract class TextBasedInput[T] extends Control[T] {
    def validate(s: String): Either[String, T]
  }
}
