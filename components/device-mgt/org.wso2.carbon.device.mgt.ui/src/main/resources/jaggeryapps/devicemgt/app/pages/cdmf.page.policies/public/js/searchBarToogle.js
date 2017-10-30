$(document).ready(function() {

    var path = window.location.href; // because the 'href' property of the DOM element is the absolute path
    $('ul.nav a').each(function() {
        if (this.href === path) {
            $(this).addClass('active');
        }
    });

    $('div.submenu a').each(function() {
        if (this.href === path) {
            $(this).addClass('active');
        }
    });

});