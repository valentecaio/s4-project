<?php
    try{
        $bdd = new PDO('mysql:host=localhost;dbname=web18_main;charset=utf8', 'root', '');
    }catch(Exception $e){
        die('Erreur : '.$e->getMessage());
    }

    if (!isset($_GET['status'])) {
        $req_balade = $bdd->query('SELECT id_balade, nom, theme, description FROM balade WHERE status = \'accepte\'');
    } else {
        $req_balade = $bdd->query('SELECT id_balade, nom, theme, description FROM balade WHERE status = \'en_attente\'');   
    }
    
    $listbalades = array();
	// for each balade
    while ($data_balade = $req_balade->fetch()) {
		// get array of points
        $req_points = $bdd->prepare('SELECT id_point, p.nom, latitude, longitude, p.description FROM point as p, balade as b, contenu_parcours as c WHERE b.status = \'accepte\' and p.id_point=c.id_p and b.id_balade=c.id_b and b.id_balade = :id_balade');
        $req_points->execute(array('id_balade' => $data_balade['id_balade']));
		
		// fetch array of points
		$listpoints = array();
        while ($data_point = $req_points->fetch()) {
            $listpoints[] = array('id' => $data_point['id_point'], 'name' => $data_point['nom'], 'lat' => $data_point['latitude'], 'lon' => $data_point['longitude']);
        }
        $req_points->closeCursor();
		
		// generate new entry to array of balades
        $listbalades[] = array('id' => $data_balade['id_balade'], 'name' => $data_balade['nom'], 'theme' => $data_balade['theme'], 'txt' => $data_balade['description'], 'points' => $listpoints);
    }
    $req_balade->closeCursor();
    echo json_encode($listbalades);
?>
