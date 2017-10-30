$(document).ready(function() {
    $("#searchBox, #group-grid_filter").hide();
    $('#searchToggleBtn').click(function() {
        $("#searchBox, #group-grid_filter").toggle("fade-in");
    });
});