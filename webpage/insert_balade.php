<?php
// Connexion à la base de données
session_start();

try
{
	$bdd = new PDO('mysql:host=localhost;dbname=s4-projet50;charset=utf8', 'root', '');
}
catch(Exception $e)
{
        die('Erreur : '.$e->getMessage());
}
// Vérification de la validité des informations

/*$req = $bdd->prepare('SELECT id_point FROM point WHERE nom = :nom');
$req->execute(array(
    'nom' => $_POST['form_name']));

$resultat = $req->fetch();

if ($resultat['id_point']){
    $_SESSION['error'] = 'Point déjà existant';
    header('Location: pagePoint.php');
    exit();
}
$req->closeCursor();*/

if(strcmp($_SESSION['permission'], "admin") == 0 ){

// Insertion du message à l'aide d'une requête préparée
    $req = $bdd->prepare('INSERT INTO balade (nom, theme, status) VALUES (:nom, :theme, \'accepte\')');
    $req->execute(array(
        'nom' => $_POST['form_name'],
        'theme' => $_POST['form_latitude']));

}else{

   // Insertion du message à l'aide d'une requête préparée
    $req = $bdd->prepare('INSERT INTO balade (nom, theme) VALUES (:nom, :theme)');
    $req->execute(array(
        'nom' => $_POST['form_name'],
        'theme' => $_POST['form_latitude']));
}
$markers = json_decode($_POST['markers']);

foreach ($markers as $value) {
    $id_point = $value->id_p;
    $id_balade =
    $ordre =
    $req = $bdd->prepare('INSERT INTO contenu_parcours (id_p, id_b, ordre) VALUES (:nom, :theme, :ordre)');
        $req->execute(array(
            'id_p' => $_POST['id_point'],
            'id_b' => $_POST['id_balade']));
}

    //$req->closeCursor();
    header('Location: pagePoint.php?modal=2');
?>