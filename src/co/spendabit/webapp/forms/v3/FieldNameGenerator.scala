package co.spendabit.webapp.forms.v3

/** An implementation of `FieldNameGenerator` should provide a consistent way of generating
  * names for a given list of fields.
  */
abstract class FieldNameGenerator[F[_] <: Field[_]] {
  def withNames(fields: Seq[F[_]]): Seq[(String, F[_])]
}
