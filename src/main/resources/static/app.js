$(document).ready(function() {
    $(document).ready(function() {
	$('#myMoviesTable').DataTable({
	    "language" : {
		"lengthMenu" : "Display _MENU_ movies per page",
		"zeroRecords" : "No movies in list",
		"info" : "Showing page _PAGE_ of _PAGES_",
		"infoEmpty" : "No movies available",
		"infoFiltered" : "(filtered from _MAX_ movies)"
	    }
	});
    });
    $(".addWatchlist").click(function() {
	var id = this.id;
	$.ajax({
	    url : "/movies/watchlist/put?id=" + id,
	    success : function(result) {
		$("#" + id + ".addWatchlist").css("display", "none");
		$("#" + id + ".removeWatchlist").css("display", "block");
		$.toast({
		    title : 'Added to watchlist',
		    type : 'success',
		    delay : 3000
		});
	    }
	});
    });
    $(".removeWatchlist").click(function() {
	var id = this.id;
	$.ajax({
	    url : "/movies/watchlist/remove?id=" + id,
	    success : function(result) {
		$("#" + id + ".addWatchlist").css("display", "block");
		$("#" + id + ".removeWatchlist").css("display", "none");
		$.toast({
		    title : 'Removed from watchlist',
		    type : 'warning',
		    delay : 3000
		});
	    }
	});
    });
    $(".markWatched").click(function() {
	var id = this.id;
	var rating = $(this).data("rating");
	$(".modal").modal('hide');
	$.ajax({
	    url : "/movies/watched/put?id=" + id + "&rating=" + rating,
	    success : function(result) {
		$("#" + id + ".addWatchlist").css("display", "none");
		$("#" + id + ".removeWatchlist").css("display", "none");
		$("#" + id + ".watchedBtn").css("display", "none");
		$("#" + id + ".unmarkWatched").css("display", "block");
		$.toast({
		    title : 'Marked as watched',
		    type : 'success',
		    delay : 3000
		});
	    }
	});
    });
    $(".markWatchedRedirect").click(function() {
	var id = this.id;
	var rating = $(this).data("rating");
	$.ajax({
	    url : "/movies/watched/put?id=" + id + "&rating=" + rating,
	    success : function(result) {
		location.reload();
	    }
	});
    });
    $(".unmarkWatched").click(function() {
	var id = this.id;
	$.ajax({
	    url : "/movies/watched/remove?id=" + id,
	    success : function(result) {
		$("#" + id + ".addWatchlist").css("display", "block");
		$("#" + id + ".removeWatchlist").css("display", "none");
		$("#" + id + ".watchedBtn").css("display", "block");
		$("#" + id + ".unmarkWatched").css("display", "none");
		$.toast({
		    title : 'Unmarked as watched',
		    type : 'warning',
		    delay : 3000
		});
	    }
	});
    });
    $('.playAudio').click(function() {
	var id = this.id;

	$("#" + id + ".my_audio")[0].play();
	$("#" + id + ".playAudio").css("display", "none");
	$("#" + id + ".pauseAudio").css("display", "block");

	$("#" + id + ".my_audio").on('ended', function() {

	    $("#" + id + ".pauseAudio").css("display", "none");
	    $("#" + id + ".playAudio").css("display", "block");
	});
    });
    $('.pauseAudio').click(function() {
	var id = this.id;

	$("#" + id + ".my_audio")[0].pause();
	$("#" + id + ".pauseAudio").css("display", "none");
	$("#" + id + ".playAudio").css("display", "block");

    });
});