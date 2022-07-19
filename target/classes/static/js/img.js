var span = document.getElementsByClassName("close")[0];
var setId = document.getElementById("setId");
// When the user clicks the button, open the modal 
let modal = document.getElementById("myModal");
function addValue(event) {
	modal.style.display = "block";
	setId.value = event.target.value;
}


// When the user clicks on <span> (x), close the modal
span.onclick = function() {
	modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
	if (event.target == modal) {
		modal.style.display = "none";
	}
}