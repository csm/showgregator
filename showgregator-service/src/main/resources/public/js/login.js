function getNext() {
    var next = window.location.hash.substring(1);
    if (next) {
        document.getElementById("then").value = next;
    }
}

function login() {
    var form = document.getElementById("login");
    if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(
            function(pos) {
                document.getElementById('lat').value = pos.coords.latitude;
                document.getElementById('lon').value = pos.coords.longitude;
                form.submit();
            }, function(err) {
                form.submit();
            });
    } else {
        form.submit();
    }
}
