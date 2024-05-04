$(document).ready(function () {
    $('#sort').DataTable();
});

function syncFromLifeMember(id, membershipExpiryDate) {
	let element = document.getElementById(id);
	let annualMembershipAmount = document.getElementById("annualMembershipAmount").innerHTML;
	let lifetimeMembershipAmount = document.getElementById("lifetimeMembershipAmount").innerHTML;
	if(element.value == "Yes") {
		document.getElementById("membershipExpiryDate").value = membershipExpiryDate;
		document.getElementById("membershipExpiryDate").setAttribute("readonly", "readonly");
		document.getElementById("receipts0.amount").value = lifetimeMembershipAmount;
		document.getElementById("receipts0.paymentFor").value = "LifeTimeMembership";
	} else if(element.value == "No") {
		document.getElementById("membershipExpiryDate").value = membershipExpiryDate;
		document.getElementById("membershipExpiryDate").removeAttribute("readonly");
		document.getElementById("receipts0.amount").value = annualMembershipAmount;
		document.getElementById("receipts0.paymentFor").value = "AnnualMembership";
	}
}

function syncFromPaymentFor(id, lifetimeMembershipExpiryDate, annualMembershipExpiryDate) {
	let element = document.getElementById(id);
	let annualMembershipAmount = document.getElementById("annualMembershipAmount").innerHTML;
	let lifetimeMembershipAmount = document.getElementById("lifetimeMembershipAmount").innerHTML;
	if(element.value == "LifeTimeMembership") {
		document.getElementById("amount").value = lifetimeMembershipAmount;
		document.getElementById("membershipExpiryDateBlock").removeAttribute("hidden");
		document.getElementById("membershipExpiryDate").setAttribute("readonly", "readonly");
		document.getElementById("membershipExpiryDate").value = lifetimeMembershipExpiryDate;
	} else if(element.value == "AnnualMembership") {
		document.getElementById("amount").value = annualMembershipAmount;
		document.getElementById("membershipExpiryDateBlock").removeAttribute("hidden");
		document.getElementById("membershipExpiryDate").removeAttribute("readonly");
		document.getElementById("membershipExpiryDate").value = annualMembershipExpiryDate;
	} else {
		document.getElementById("amount").value = "";
		document.getElementById("membershipExpiryDateBlock").setAttribute("hidden", "hidden");
	}
}

$(document).ready(function () {
	var date = new Date();																													// local date
	var year = date.getFullYear();
	var month = date.getMonth() + 1;																										// Jan=0
	var dayOfMonth = date.getDate();																										// 1 to 31
	month = month < 10 ? ("0"+month) : month;
	dayOfMonth = dayOfMonth < 10 ? ("0"+dayOfMonth) : dayOfMonth;
	var dateToBeAppended = year +"-"+ month +"-"+ dayOfMonth;
	if(document.getElementById("paymentDate") == null) {
    	document.getElementById("receipts0.paymentDate").max = dateToBeAppended;
	} else {
	    document.getElementById("paymentDate").max = dateToBeAppended;
	}
});
