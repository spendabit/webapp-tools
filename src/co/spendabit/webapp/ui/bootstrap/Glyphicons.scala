package co.spendabit.webapp.ui.bootstrap

trait Glyphicons {

  protected def glyphicon(icon: String) =
    <span class={ s"glyphicon glyphicon-$icon" } aria-hidden="true"></span>
}
