var arr = document.getElementsByClassName("slide-img");
let i = 1;
function increment() {
	console.log(i);
	for (let j = 0; j < arr.length; j++) {
		arr[j].style.display = "none";
	}
	arr[i].style.display = "block";
	i++;
	if (arr.length - 1 < i) {
		i = 0;
	}
}
function decrement() {
	console.log(i);
	for (let j = 0; j < arr.length; j++) {
		arr[j].style.display = "none";
	}
	arr[i].style.display = "block";
	i--;
	if (0 > i) {
		i = arr.length - 1;
	}
	//document.getElementById("img").src = arr[i];
}
let interval = setInterval(increment, 2000);
document.querySelector(".image-slider").addEventListener("mouseover", () => {
	clearInterval(interval);
});
 document.querySelector(".image-slider").addEventListener("mouseleave", () => {
	interval = setInterval(increment, 2000);
})