$(document).ready(function(){
    $("#button_reset").click(function() { validate(); })
})

$(document).on('keypress', function(e) { if(e.which == 13) {validate();} })

function validate() {
    if ($('#reset_password_1').val().length < 6) {
        $('#reset_password_1').focus();
        $("#prompt").html('Please enter at least 6 characters');
    }
    else if($('#reset_password_1').val() != $('#reset_password_2').val()) {
         $('#reset_password_1').focus();
         $("#prompt").html('Passwords do not match');
    }
    else {
        hash();
    }
}

function hash() {

    $.ajax({
        type: "POST",
        url: "/hash/json",
        headers: {"CSRF-Token" : $("#csrf").val()},
        data: JSON.stringify({"token": $("#token").val(),"password": $("#reset_password_1").val()}),
        contentType: "application/json; charset=utf-8",
        success: function(obj) {
            if (obj.status == 'success') {
                $("#proceed").removeClass("d-none");
                $("#reset").addClass("d-none");
            }
            else {
                console.log(obj.message)
            }

        },
        dataType: "json"
    });

}