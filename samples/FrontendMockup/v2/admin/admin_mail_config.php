<?php
include '../core/config.php';
include '../core/RESTCalls.php';
include '../core/db_connection.php';



$result = mysql_query("SELECT * FROM `emailconfiguration`");

while($row = mysql_fetch_array($result)) {
    echo "
        <form action='update_mail_config.php' method='post'>
            <input type='hidden' name='id' value='".$row['id']."'><br>
            
            receivermailaddress: <input type='text' name='receivermailaddress' value='".$row['receivermailaddress']."'><br>
            subject: <input type='text' name='subject' value='".$row['subject']."'><br>
            message: <textarea type='text' name='message'rows='2' cols='20'>".$row['message']."</textarea><br>
            <input type='submit'>
        </form>
    ";
    //print_r($row);
}


