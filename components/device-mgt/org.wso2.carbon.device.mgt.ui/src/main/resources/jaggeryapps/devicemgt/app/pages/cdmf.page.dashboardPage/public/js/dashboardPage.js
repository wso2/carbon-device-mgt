$(document).ready(function() {
    $('#loading-content').show();
    var path = window.location.href;
    $('ul.nav a').each(function() {
        var url = this.href;
        if (url.endsWith("/devicemgt/")) {
            $(this).addClass('active');
        }
    });
});