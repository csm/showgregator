function validate_and_register() {
    document.getElementById('bad_email').innerHTML = '';
    document.getElementById('password_mismatch').innerHTML = '';
    document.getElementById('empty_password').innerHTML = '';
    console.log("validating...");
    var form = document.getElementById('register');
    var email = document.getElementById('email').value;
    console.log("email is " + email);
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    if (!re.test(email)) {
        var oops = document.getElementById('bad_email');
        oops.innerHTML = "Doesn't look like a valid email address."
    } else {
        var password = document.getElementById('password').value;
        var confirm = document.getElementById('confirm').value;
        if (password.length > 0) {
            if (password == confirm) {
                if ("geolocation" in navigator) {
                    navigator.geolocation.getCurrentPosition(
                        function(pos) {
                            document.getElementById('lat').value = pos.coords.latitude;
                            document.getElementById('lon').value = pos.coords.longitude;
                            form.submit();
                        }, function(err) {
                            form.submit();
                        }
                    );
                } else {
                    form.submit();
                }
            } else {
                var oops = document.getElementById('password_mismatch');
                oops.innerHTML = "Passwords don't match.";
            }
        } else {
            var oops = document.getElementById('empty_password');
            oops.innerHTML = "Please put some kind of password in."
        }
    }
    return false;
}