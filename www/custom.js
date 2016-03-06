
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
		 	        "pre": "<div class='rawPublications'>",
		        	"field": "publications.edition",
		    	},
		 	    {
		 	        "pre": "|",
		        	"field": "publications.rarity"
		    	},
		 	    {
		 	        "pre": "|",
		        	"field": "publications.editionImage",
		    	},
		 	    {
		 	        "pre": "|",
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
		var editions = textSplit[0].split(',');
		var rarities = textSplit[1].split(',');
		var images = textSplit[2].split(',');
		var editionImages = 'null';
		if (textSplit.length == 4) {
			editionImages = textSplit[2].split(',');
			images = textSplit[3].split(',');
		}

		var html = ''
		var showMoreLink = false;
		editions.forEach(function(element, index, array) {
			var title = element + " - " + rarities[index];
			var cssClass = '';
			if (index > 19) {
				cssClass = ' hidden';
				showMoreLink = true;
			}

			if (editionImages[index].trim() !== "undefined" && editionImages[index].indexOf('null') == -1) {
				html += "<div class='" + cssClass + "'>";
				html += "<a title=\"" + title + "\" href='" + images[index] + "'>";
				html += "<img src='" + editionImages[index] + "' alt='" + title + "'/> ";
				html += "</a>";
				html += "</div> ";
			} else {
				html += "<div class='label label-important" + cssClass + "'>";
				html += "<a title=\"" + title + "\" href='" + images[index] + "'>";
				html += element;
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
			html = html.replace(re, '<img alt="' + color + '" src="img/hd/' + color + '.jpeg" title="' + color + '" width="16">');
		});
		$(this).html(html);
	});
});

function getColors() {
	return getGuildColors().concat(getLifeColors()).concat([
		'2B', '2U', '2R', '2W', '2G',
		'B',  'U',  'R',  'W',  'G',  'X'
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
