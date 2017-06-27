<?php 
//echo "modify settings";
session_start();

// Connexion à la base de données
try
{
	$bdd = new PDO('mysql:host=localhost;dbname=web18_main;charset=utf8', 'root', '');
}
catch(Exception $e)
{
        die('Erreur : '.$e->getMessage());
}


$req = $bdd->prepare('UPDATE point SET nom = :nom, latitude = :latitude, longitude = :longitude, description = :description WHERE id_point = :id_point');
$req->execute(array(
	'id_point' => $_POST['form_id'],
	'nom' => $_POST['form_name'],
	'latitude' => $_POST['form_latitude'],
	'longitude' => $_POST['form_longitude'],
	'description' => $_POST['form_comment']));

if ( !$_FILES["file_upload"]["name"] == ''){
	$id_point_ref = $_POST['form_id'];
	$target_dir = 'uploads/';
-   $target_file = $target_dir . basename($_FILES["file_upload"]["name"]);
	$fileType = pathinfo($target_file,PATHINFO_EXTENSION);
	$valid_extensions = array('jpeg', 'jpg', 'png', 'gif', 'mp3', 'wma', 'mp4');
	if(in_array($fileType, $valid_extensions)){
	    move_uploaded_file($_FILES["file_upload"]["tmp_name"], $target_file);
	    $req = $bdd->prepare('INSERT INTO media (chemin, id_point_ref) VALUES (:chemin, :id_point_ref)');
	    $req->execute(array(
	        'chemin' => basename($_FILES["file_upload"]["name"]),
	        'id_point_ref' => $id_point_ref));
	}
	else {
	    echo "Sorry, there was an error uploading your file.";
	}
}
	
	header('Location: pageMain.php');
	exit();
?>