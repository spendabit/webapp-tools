package co.spendabit.webapp.integrations

package object google {

  def analyticsScriptElem(trackingID: String) =
    <script type="text/javascript">
      { xml.Unparsed(analyticsJavaScriptSnippet(trackingID)) }
    </script>

  def analyticsJavaScriptSnippet(trackingID: String) =
    s"""
      (function(i,s,o,g,r,a,m){
        i['GoogleAnalyticsObject']=r;
        i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();
        a=s.createElement(o),m=s.getElementsByTagName(o)[0];
        a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
      ga('create', '$trackingID', 'auto');
      ga('send', 'pageview');
    """
}

