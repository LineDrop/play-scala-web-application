$(document).ready(function(){
    $("#button_sign_in").click(function() { validate(); })
})

$(document).on('keypress', function(e) { if(e.which == 13) { validate();} })

function validate() {
    var valid = true;

    if ( $('#password').val().length < 6 && $('#password').val() != 'Password' ) {
            $('#password').focus();
            valid = false;
    }

    if ( $('#email').val().indexOf("@") == -1 ) {
            $('#email').focus();
            valid = false;
    }

    if(valid == true) sign_in();
}

function sign_in() {

    $.ajax({
        type: "POST",
        url: "/authenticate/json",
        headers: {"CSRF-Token" : $("#csrf").val()},
        data: JSON.stringify({"email": $("#email").val(),"password": $("#password").val()}),
        contentType: "application/json; charset=utf-8",
        success: function(obj) {
                if (obj.status == 'success')
                    window.location.replace("/subscribers");
                else console.log(obj.message);
            },
        dataType: "json"
    });

}