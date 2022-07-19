function addBorder() {
	var images = document.getElementsByClassName("target-img");
	for (let i = 0; i < images.length; i++) {
		images[i].style.border = "none";
		console.log("something happens")
	}
}
async function changeImage(event) {
	var targetImage = document.getElementById("main-image");
	targetImage.src = event.target.src;
	await addBorder();
	event.target.style.border = "2px solid blue";
	console.log("ran after")
}
function calculatePrice() {
	const quantity = document.getElementById("quantity").value;
	const price = document.getElementById("price").innerText.trim();
	const total = quantity * price;
	document.getElementById("total").innerHTML = "Total Price  " + total;
}