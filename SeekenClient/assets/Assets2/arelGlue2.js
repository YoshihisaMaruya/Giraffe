
arel.sceneReady(function()
{
	console.log("sceneReady");

	//set a listener to tracking to get information about when the image is tracked
	arel.Events.setListener(arel.Scene, function(type, param){trackingHandler(type, param);});
                
	//get the metaio man model reference
	var metaioMan = arel.Object.Model3D.create("1","metaioman.md2","metaioman.png");
	metaioMan.setVisibility(true);
	metaioMan.setCoordinateSystemID(1);
	metaioMan.setScale(new arel.Vector3D(4.0,4.0,4.0));
	arel.Scene.addObject(metaioMan);

	var truck = arel.Object.Model3D.create("2","truck.obj","truck.png");
	truck.setVisibility(false);
	truck.setCoordinateSystemID(1);
	truck.setScale(new arel.Vector3D(2.0,2.0,2.0));
	var truckRotation = new arel.Rotation();
	truckRotation.setFromEulerAngleDegrees(new arel.Vector3D(90.0,0.0,180.0));
	truck.setRotation(truckRotation);
	arel.Scene.addObject(truck);

                
    var image = arel.Object.Model3D.createFromImage("image","frame.png");
    image.setVisibility(false);
    image.setCoordinateSystemID(1);
    image.setScale(new arel.Vector3D(3.0,3.0,3.0));
    arel.Scene.addObject(image);
                
    var movie = arel.Object.Model3D.createFromMovie("movie","demo_movie.alpha.3g2"); //add alpha here
    movie.setVisibility(false);
    movie.setCoordinateSystemID(1);
    movie.setScale(new arel.Vector3D(2.0,2.0,2.0));
    var movieRotation = new arel.Rotation();
    movieRotation.setFromEulerAngleDegrees(new arel.Vector3D(0.0,0.0,-90.0));
    movie.setRotation(movieRotation);
    arel.Scene.addObject(movie);

});

function trackingHandler(type, param)
{
	//check if there is tracking information available
	if(param[0] !== undefined)
	{
		//if the pattern is found, hide the information to hold your phone over the pattern
		if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_TRACKING)
		{
			$('#info').fadeOut("fast");
            arel.Scene.getObject("movie").startMovieTexture();
		}
		//if the pattern is lost tracking, show the information to hold your phone over the pattern
		else if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_NOTTRACKING)
		{
			$('#info').fadeIn("fast");
            arel.Scene.getObject("movie").pauseMovieTexture();
		}
	}
};

function clickHandler()
{
    var idClicked = $("#radio :radio:checked").attr('id');
    if (idClicked == 'radio1')
    {
        arel.Scene.getObject("1").setVisibility(true);
        arel.Scene.getObject("image").setVisibility(false);
        arel.Scene.getObject("2").setVisibility(false);
        arel.Scene.getObject("movie").setVisibility(false);
        arel.Scene.getObject("movie").stopMovieTexture();
    }
    if (idClicked == 'radio2')
    {
        arel.Scene.getObject("1").setVisibility(false);
        arel.Scene.getObject("image").setVisibility(true);
        arel.Scene.getObject("2").setVisibility(false);
        arel.Scene.getObject("movie").setVisibility(false);
        arel.Scene.getObject("movie").stopMovieTexture();
    }
    if (idClicked == 'radio3')
    {
        arel.Scene.getObject("1").setVisibility(false);
        arel.Scene.getObject("image").setVisibility(false);
        arel.Scene.getObject("2").setVisibility(true);
        arel.Scene.getObject("movie").setVisibility(false);
        arel.Scene.getObject("movie").stopMovieTexture();
    }
    if (idClicked == 'radio4')
    {
        arel.Scene.getObject("1").setVisibility(false);
        arel.Scene.getObject("image").setVisibility(false);
        arel.Scene.getObject("2").setVisibility(false);
        arel.Scene.getObject("movie").setVisibility(true);
        arel.Scene.getObject("movie").startMovieTexture();
    }
};