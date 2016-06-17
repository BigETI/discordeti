var butlers = [{
		name : "Butler",
		gender : "M",
		purpose : ["laptop name"],
		source : ["Artemis Fowl"],
		altNames : ["Domovoi", "Domovoi Butler"]
	}, {
		name : "Jarvis",
		gender : "M",
		purpose : ["local code storage/repo"],
		source : ["Iron Man"],
		altNames : ["Just A Rather Very Intelligent System", "J.A.R.V.I.S.", "JARVIS"]
	}, {
		name : "Friday",
		gender : "M",
		source : ["Friday by Robert A Heinlein", "Robinson Crusoe", "Iron Man"],
		altNames : ["F.R.I.D.A.Y.", "FRIDAY", "Girl Friday"],
		notes : "Friday appears to be a male name in modern culture except when referred to as an assistant"
	}, {
		name : "Jocasta",
		gender : "M",
		purpose : ["singularity"],
		source : ["Ultron Comics", "Iron Man"],
		notes : "Jocasta is a female in the Ultron series, but appears to be a male name in modern culture."
	}, {
		name : "Homer",
		gender : "M",
		source : ["Iron Man"],
		altNames : ["Heuristically Operative Matrix Emulation Rostrum", "H.O.M.E.R.", "HOMER"]
	}, {
		name : "Plato",
		gender : "M",
		source : ["Iron Man"],
		altNames : ["Piezo-Electrical Logistic Analytical Tactical Operator", "P.L.A.T.O.", "PLATO"]
	}, {
		name : "Virgil",
		gender : "M",
		source : ["Iron Man"],
		altNames : ["Virtual Integrated Rapidly-evolving Grid-based Intelligent Lifeform", "V.I.R.G.I.L.", "VIRGIL"]
	}, {
		name : "Regis",
		gender : "M",
		purpose : ["smartHAus controller"],
		source : ["Masque"]
	}, {
		name : "Pepper",
		gender : "F",
		source : ["Tony Stark's personal assistant in Iron Man, who later came to run Stark Industries directly as CEO."],
		altNames : ["Pepper Potts", "Virginia", "Potts"]
	}, {
		name : "Nigel",
		gender : "M"
	}, {
		name : "Cortana",
		gender : "F",
		source : ["The assistant ai in Halo, later the voice assistant in Microsoft's products."]
	}, {
		name : "Alfred",
		gender : "M",
		source : ["Batman"]
	}, {
		name : "Jeeves",
		gender : "M",
		purpose : ["network router"]
	}, {
		name : "Desmond",
		gender : "M",
		purpose : ["firewall"]
	}, {
		name : "James",
		gender : "M"
	}, {
		name : "Lurch",
		gender : "M",
		purpose : ["door camera"],
		source : ["Adams Family"]
	}, {
		name : "Reginald",
		gender : "M"
	}, {
		name : "Jewels",
		gender : "F/M",
		note : "The name was predominately male thru the early 1900s, but has been overtaken as female in 1990's. Most people over 30 (as of 2016) are male. (wolframalpha)"
	}, {
		name : "Niles",
		gender : "M"
	}, {
		name : "Jules",
		gender : "F/M",
		note : "Not to be confused with Jewels, this name has a relatively stable ratio of 6/1 male female thru history. (wolframalpha)"
	}, {
		name : "Ada",
		gender : "F",
		purpose : ["digital whiteboard"]
	}, {
		name : "Max",
		gender : "M",
		note : "This name has a small portion of female usage, but its at a rate of 1/300 in 2016. (wolframalpha)"
	}, {
		name : "Abbot",
		gender : "M"
	}, {
		name : "Astro",
		gender : "M"
	}, {
		name : "Rosie",
		gender : "F",
		purpose : ["cleanup routine", "roomba", "automatic dishwashing machine"],
		source : ["The Jetsons"]
	}, {
		name : "Alice",
		gender : "F",
		source : ["Early AI chatbot experiment."],
		altNames : ["The Artificial Linguistic Internet Computer Entity", "A.L.I.C.E.", "ALICE"]
	}, {
		name : "Watson",
		gender : "M",
		purpose : ["Fitbit"],
		source : ["Sherlock Holmes"],
		altNames : ["Dr. Watson"]
	}, {
		name : "Frank",
		gender : "M",
		purpose : ["smart phone"],
		source : ["Ze Frank"]
	}, {
		name : "Geoffrey",
		gender : "M",
		purpose : ["sarcasm detection"],
		source : ["The fresh prince of Bel Air"]
	}, {
		name : "Ross",
		gender : "M",
		source : ["AI lawer - http://www.rossintelligence.com/lawyers/"],
		altNames : ["ROSS"],
		note : "Also a popular last name, ranking 89th on wolframalpha."
	}, {
		name : "Siri",
		gender : "F",
		source : ["The name of apple's voice assistant application. Norwegian for 'beautiful woman who leads you to victory' - Dag Kittalaus wanted to call his daughter if he had one."]
	}, {
		name : "Alexa",
		gender : "F",
		source : ["Amazon's digital assistant from the 'Echo'. Can also be changed to 'Amazon' or 'Echo'."]
	}, {
		name : "Glass",
		gender : "?",
		source : ["Google's 'glass' headware could respond to this name. Also known as 'Ok, Google' on mobile"]
	}, {
		name : "Samantha",
		gender : "F",
		source : ["The popular movie about an AI assistant 'Her'"]
	}, {
		name : "Tay",
		gender : "F",
		source : ["A chatBot experiment by Microsoft that hit big in pop-culture's imagination. The name is possibly a nod to the character in @SwiftOnSecurity's twitter fiction."],
		altNames : ["TayAI"]
	}
]
var i = Math.floor(Math.random() * butlers.length);
print(butlers[i].name + " (" + butlers[i].gender + ")");
if (typeof butlers[i].purpose !== 'undefined') {
	print("Purposes:");
	for (var j = 0; j < butlers[i].purpose.length; j++)
		print("\t" + butlers[i].purpose[j]);
}
if (typeof butlers[i].source !== 'undefined') {
	print("Sources:");
	for (var j = 0; j < butlers[i].source.length; j++)
		print("\t" + butlers[i].source[j]);
}
if (typeof butlers[i].altNames !== 'undefined') {
	print("Alternative names:");
	for (var j = 0; j < butlers[i].altNames.length; j++)
		print("\t" + butlers[i].altNames[j]);
}
