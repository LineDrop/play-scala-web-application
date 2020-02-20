function search() {
    var value = $("#search").val().toLowerCase();

    $("#searchable_list li.searchable").filter(function() {
        $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
    });
}
