
function range(start, end) {
    var foo = [];
    for (var i = start; i >= end; i--) {
        foo.push(i);
    }
    return foo;
}

// facetview
jQuery(document).ready(function($) {
  $('.facet-view-simple').each(function() {
	  $(this).facetview({
	    search_url: searchUrl,
	    search_index: 'magic',
	    datatype: 'json',
	    facets: [
	        {'field': 'colors.exact', 		'display': 'Color'},
	        {'field': 'devotions', 			'display': 'Devotion'},
	        {'field': 'convertedManaCost',	'display': 'Converted mana cost'},
	        {'field': 'type',				'display': 'Type'},
	        {'field': 'abilities.exact',	'display': 'Ability'},
	        {'field': 'power',				'display': 'Power'},
	        {'field': 'toughness',			'display': 'Toughness'},
	        {'field': 'rarities',			'display': 'Rarity'},
	        {'field': 'priceRanges.exact',	'display': 'Price'},
	        {'field': 'editions.exact',		'display': 'Set'},
	        {'field': 'blocks.exact',		'display': 'Block'},
	        {'field': 'formats.exact',		'display': 'Format'},
	        {'field': 'artists.exact',		'display': 'Artist'},
	    ],
	    paging: { size: 10 },
	    pager_on_top: true,
	    default_operator: "AND",
	    sort: ['_uid'],
	    result_display: [
			[
		 	    {
		 	        "pre": "<span class='castingCost'>",
	 	        	"field": "castingCost",
		 	        "post": "</span>&nbsp;"
	 	    	},
		 	    {
		 	        "pre": "<strong class='title'>",
		 	        "field": "title",
			 	    "post": "</strong>"
		 	    },
		 	    {
		 	        "pre": "<strong class='title'> &mdash; ",
		 	        "field": "frenchTitle",
		 	        "post": "</strong>"
		 	    },
	 	    ],
	 	    [
		 	    {
		 	        "pre": "<span class='label label-info'>",
		        	"field": "type",
		 	        "post": "</span>"
		    	},
		 	    {
		 	    	"pre": "&nbsp;<span class='label label-success'>",
		        	"field": "power",
		 	        "post": " &ndash; "
		    	},
		 	    {
		        	"field": "toughness",
		        	"post": "</span>"
		    	},
		    ],
	 	    [
		 	    {
		 	        "pre": "<pre class='description'>",
		        	"field": "description",
		 	        "post": "</pre>"
		    	},
		 	    {
		 	        "pre": "<div class='rawPublications'>edition:",
		        	"field": "publications.edition",
		    	},
		 	    {
		 	        "pre": "|rarity:",
		        	"field": "publications.rarity"
		    	},
		 	    {
		 	        "pre": "|editionImage:",
		        	"field": "publications.editionImage",
		    	},
                {
                    "pre": "|price:",
                    "field": "publications.price",
                },
                {
                    "pre": "|image:",
                    "field": "publications.image",
                    "post": "</div>",
                }
		    ]
	     ]
	  });
  });

  $('.help').fancybox();
  $('.help').click(function(event) {
	  if ($(event.target).hasClass('btn-success') ||
		  $(event.target).hasClass('help-lang-txt')) {
		  $('.help-lang').toggle();
	  }

	  if ($(this).is(':visible')) {
		  event.stopPropagation();
	  }
  });
  $('.help-button').click(function() {
	  $('.help').click();
  });
});

$(document).ajaxComplete(function() {
	// transformation des publications
	$('.rawPublications').each( function() {
		var textSplit = $(this).text().split('|');

		var editions = extract("edition:", textSplit);
		var rarities = extract("rarity:", textSplit);
		var images = extract("image:", textSplit);
		var prices = extract("price:", textSplit);
        var editionImages = extract("editionImage:", textSplit);

        /*
		console.log("editions", editions)
		console.log("rarities", rarities)
		console.log("images", images)
		console.log("prices", prices)
		console.log("editionImages", editionImages)
		*/

		var html = ''
		var showMoreLink = false;
		editions.forEach(function(edition, index, array) {
			var title = edition + " - " + rarities[index];
			var cssClass = '';
			if (index > 19) {
				cssClass = ' hidden';
				showMoreLink = true;
			}

			var price = prices[index];
			if (typeof price === "undefined" || price === "undefined") price = ""
			else price = "<span class=\"label label-info\">$" + round(price) + "</span>"

            var editionImage = editionImages[index]
			if (typeof editionImage === "undefined" || editionImage === "undefined") {
				html += "<div class='label label-important" + cssClass + "'>";
				html += "<a title=\"" + title + "\" href='" + images[index] + "'>";
				html += edition + price;
				html += "</a>";
				html += "</div> ";
			} else {
				html += "<div class='pic " + cssClass + "'>";
				html += "<a title=\"" + title + "\" href='" + images[index] + "'>";
				html += "<img src='" + editionImage + "' alt='" + title + "'/>" + price;
				html += "</a>";
				html += "</div> ";
			}
		});

		if (showMoreLink) {
			html += "<div class='label label-warning showmore'>Show more ...</div>"
		}

		$(this).html(html);
	});

	$('.thumbnail').click( function() {
		$(this).siblings(".rawPublications").find("a:first").click();
	});

	$('.showmore').click( function() {
		$(this).hide();
		$(this).siblings('.hidden').removeClass('hidden');
	});

	// initialisation de la gallery des images des cartes
	$('.rawPublications a').attr('rel', 'gallery');
	$('.rawPublications a').fancybox();

	// affichage des symboles spéciaux
	$('.castingCost').each( function() {
		var html = $(this).html() + ' ';
		var re = new RegExp('/', 'g');
		html = html.replace(re, '');

		getColors().forEach( function(color) {
			var re = new RegExp(color + ' ', 'g');
			html = html.replace(re, '<img alt="' + color + '" src="img/hd/' + color + '.jpeg" title="' + color + '" width="16">');
		});
		$(this).html(html);
	});

	$('.description').each( function() {
		var html = $(this).html();

		getGuildColors().concat(getLifeColors()).forEach( function(color) {
			var re = new RegExp('\\{\\(' + color[0].toLowerCase() + '/' + color[1].toLowerCase() + '\\)\\}', 'g');
			html = html.replace(re, '{' + color + '}');
		});

		getColors().concat(getSpecialSymbols()).forEach( function(color) {
			var re = new RegExp('\\{' + color + '\\}', 'g');
			html = html.replace(re, '<img class="mtgIcon" alt="' + color + '" src="img/hd/' + color + '.jpeg" title="' + color + '" width="16">');
		});
		$(this).html(html);
	});
});

function extract(field, data) {
    for (index in data) {
        var d = data[index]
        if (d.startsWith(field)) {
            var split = d.replace(field, "").split(",")
            return split.map(function(s) { return s.trim() })
        }
    }
    return []
}

function getColors() {
	return getGuildColors().concat(getLifeColors()).concat([
		'2B', '2U', '2R', '2W', '2G',
		'B',  'U',  'R',  'W',  'G',  'X',  'C'
	]).concat(range(16, 0));
}

function getGuildColors() {
	return [
		'WU', 'RW', 'UB', 'BG', 'RG',
		'UR', 'WB', 'BR', 'GW', 'GU'
	]
}

function getLifeColors() {
	return [
		'BP', 'UP', 'RP', 'WP', 'GP'
	]
}

function getSpecialSymbols() {
	return ['T', 'Q', 'S'];
}

function round(value) {
  var exp = 2;
  var valueAsDouble = parseFloat(value)
  if (valueAsDouble > 1000) exp = -2
  else if (valueAsDouble > 100) exp = -1
  else if (valueAsDouble > 10) exp = 0
  else if (valueAsDouble > 1) exp = 1

  value = +value;
  exp = +exp;

  if (isNaN(value) || !(typeof exp === 'number' && exp % 1 === 0))
    return NaN;

  // Shift
  value = value.toString().split('e');
  value = Math.round(+(value[0] + 'e' + (value[1] ? (+value[1] + exp) : exp)));

  // Shift back
  value = value.toString().split('e');
  return +(value[0] + 'e' + (value[1] ? (+value[1] - exp) : -exp));
}