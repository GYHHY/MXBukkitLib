/*
 * Copyright (c) 2018-2019 Karlatemp. All rights reserved.
 * Reserved.FileName: ExecuterEX.java@author: karlatemp@vip.qq.com: 19-9-18 下午5:54@version: 2.0
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.mcres.gyhhy.MXLib.bukkit.cmd;

import java.lang.annotation.Annotation;
import org.bukkit.command.CommandSender;

/**
 *
 * @author 32798
 */
public class ExecuterEX extends Executer {

    private SubCommandEX $defexec;
    private SubCommandEX $nosub;

    public void reg(String a, SubCommandEX b) {
        switch (a) {
            case Variable.COMMAND_CMD_DEF: {
                $defexec = b;
                return;
            }
            case Variable.COMMAND_NOSUB: {
                $nosub = b;
                return;
            }
        }
        super.reg(a, b);
    }

    private String per;

    @SuppressWarnings({"unchecked", "rawtypes", "OverridableMethodCallInConstructor"})
    public ExecuterEX(Class cx) {
        if (cx == null) {
            return;
        }
        Annotation anc = cx.getAnnotation(Command.class);
        if (anc == null) {
            throw new IllegalArgumentException("This class is not a command class.(No Command Annotation)");
        }
        Command ec = (Command) anc;
        setPermission(ec.permission());
        reg("help", new SubCommandHelp(this));
    }

    public String getPermission() {
        return per;
    }

    public void setPermission(String p) {
        per = p;
    }

    @Override
    protected boolean nosub(CommandSender sender, org.bukkit.command.Command cmd, String ali, String[] argc) {
        if (this.$nosub != null) {
            return this.$nosub.exec(sender, cmd, ali, argc, this);
        } else {
            defexec(sender, cmd, ali, argc);
        }
        return true; //To change body of generated methods, choose Tools | Templates.
    }
    @Deprecated
    public String msg$noper = "\u00a7cYou don't have the permission to do that.";

    @Override
    protected boolean check(CommandSender sender) {
        if (per == null || per.isEmpty()) {
            return true;
        }
        if (sender.hasPermission(per)) {
            return true;
        }
        this.getLanguageTranslator().noPermission(this, sender, per);
        return false;
    }

    @Override
    protected boolean defexec(CommandSender sender, org.bukkit.command.Command cmd, String ali, String[] argc) {
        if (this.$defexec != null) {
            return this.$defexec.exec(sender, cmd, ali, argc, this);
        } else {
            return this.getLanguageTranslator().promptToHelp(this, sender, cmd, ali, argc);
        }
    }

}
