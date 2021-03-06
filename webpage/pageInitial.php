<?php session_start(); ?>
<!DOCTYPE html>
<html lang="en">
	<head>
		<title>Balade à Brest</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<link rel='stylesheet' type='text/css' href="style_pageInitial.css">
		<script src="lib/jquery.min.js"></script>
		<script src="lib/bootstrap.min.js"></script>
		<link rel="shortcut icon" type="image/x-icon" href="./images/blue_marker2.png">
	</head>
	
	<?php
		if (!empty($_SESSION['error'])){
			echo '<script type="text/javascript">alert("'.$_SESSION['error'].'");</script>';
			//echo $_SESSION['error'];
			unset($_SESSION['error']);
		}
		if(isset($_GET['modal'])){ ?>
		<script type="text/javascript">
			$(document).ready(function(){
				$('#myModal').modal('show');
			});
		</script>
	<?php } ?>
	
	<body>
		<?php include 'navigationBar.php';	?>
		<div class="container-fluid">
			<div class="wb">
				<h1>Balade à Brest</h1>
				<p class="lead">L'objectif du projet est de fournir aux élèves du dispositif relais<br>un système qui leur permettra de s'impliquer dans un projet d'une<br> application de balade en réalité augmentée à Brest.</p>
				<p><a href="pageMain.php" class="btn btn-default" role="button">Essayer</a></p>
			</div>
		</div>
		
	</body>
</html>
