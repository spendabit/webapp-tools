package co.spendabit.webapp.forms.v3

import scala.language.higherKinds

class DefaultNameGenerator[F[_] <: Field[_]] extends FieldNameGenerator[F] {
  def withNames(fields: Seq[F[_]]): Seq[(String, F[_])] =
    fields.map { f =>
      val name: String = f.label.toLowerCase.map(c => if (c.isLetterOrDigit) c else '-')
      name -> f
    }
}
