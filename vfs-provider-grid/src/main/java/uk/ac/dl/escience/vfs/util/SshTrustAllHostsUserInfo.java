/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.dl.escience.vfs.util;

import com.jcraft.jsch.UIKeyboardInteractive;

/**
 * Class used to trust all ssh hosts.
 *
 * @author David Meredith
 */
public class SshTrustAllHostsUserInfo implements com.jcraft.jsch.UserInfo,
    UIKeyboardInteractive {

    String passphrase;
    String password;


    public void setPassphrase(String p) {
        this.passphrase = p;
    }


    public void setPassword(String p) {
        this.password = p;
    }


    public String getPassphrase() {
        return this.passphrase;
    }


    public String getPassword() {
        return this.password;
    }


    public boolean promptPassword(String string) {
        return false;
    }


    public boolean promptPassphrase(String string) {
        return false;
    }


    /**
     * Always trus the host.
     *
     * @param message
     * @return true
     */
    public boolean promptYesNo(String message) {
        // Do you trust the host - return true if you do else return false.
        /*Object[] options = {"yes", "no"};
        int foo = javax.swing.JOptionPane.showOptionDialog(null,
        message, "Dave do you Trust this Host - Warning",
        javax.swing.JOptionPane.DEFAULT_OPTION,
        javax.swing.JOptionPane.WARNING_MESSAGE,
        null, options, options[0]);
        return foo == 0;*/
        return true;
    }


    public void showMessage(String string) {
    }


    /**
     * Implementation of UIKeyboardInteractive#promptKeyboardInteractive.
     *
     * @param  destination - not used.
     * @param name - not used.
     * @param instruction - not used.
     * @param prompt - not used.
     * @param echo - not used.
     * @return the password in a size one array if there is a password
     * and if the prompt and echo checks pass.
     */
    public String[] promptKeyboardInteractive(String destination,
        String name,
        String instruction, String[] prompt, boolean[] echo) {
        //System.out.println("promptKeyboardInteractive called");
        // @param prompt - check to see if this is one in length ?
        // @param echo - check to see if the first element is false ?

        String[] ret = new String[1];
        ret[0] = this.getPassword();
        // return value is the password.
        return ret;
    }
    }
