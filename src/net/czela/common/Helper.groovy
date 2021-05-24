package net.czela.common

import groovy.sql.Sql

class Helper {
    static Properties props

/**
 * Connects to the database
*/
    static Sql newSqlInstance(String propFile, Object script, String prefix = null) {
        String name = script.getClass().getName() + ".groovy"
        props = new Properties()
        def dirs = ['./', '../','../../../']
        boolean found = false
        for(def d:dirs) {
            File f = new File(d + propFile)
            if (f.exists()) {
                props.load(new FileReader(f))
                found = true
                break;
            }
        }
        if (!found) {
            System.err.println("Properties file ${propFile} cannot be found.")
        }

        if (prefix) {
            prefix += "."
        } else {
            prefix = ""
        }

        Properties jdbcProps = new Properties()
        jdbcProps.setProperty("user", props.get("${prefix}jdbc.username".toString()) as String)
        jdbcProps.setProperty("password", props.get("${prefix}jdbc.password".toString()) as String)
        jdbcProps.setProperty('ApplicationName', name)// pgsql
        return (Sql) Sql.newInstance(props.get("${prefix}jdbc.url".toString()), jdbcProps, props.get("${prefix}jdbc.driver".toString()))
    }

    static String get(String property) {
        return props.get(property)
    }

    static String get(String property, String defaultValue) {
        String v = props.get(property)
        return v == null?defaultValue:v
    }

    static Long asLong(String longString) {
        if (notEmpty(longString)) {
            return Long.parseLong(longString)
        }
        return null
    }

    static boolean notEmpty(String s) {
        s != null && s.length() > 0;
    }

    static BigDecimal asDecimal(String decString) {
        if (notEmpty(decString)) {
            try {
                return new BigDecimal(decString)
            } catch (NumberFormatException e) {
                println("bad number = '$decString'");
                throw e
            }
        }
        return null
    }

    static String filterNumbersOnly(String s) {
        s==null?null:s.replaceAll(/[^0-9]/,'').trim()
    }

    static def fmt6(def s) {
        if (s == null) return ''
        String ss = s.toString()
        def l = ss.length()
        if (l >= 6) return s
        return "000000$ss".substring(l)
    }

    static def addAttr(def key, def val) {
        String skey = key as String
        String sval = (val as String)?.trim()?.replaceAll(/<[^<>]+>/,'')
        def sep = sval?.contains('"')?"'":(sval?.isNumber()?'':'"')

        return (skey?.length() > 0 && skey.length() > 0)?"$skey: $sep$sval$sep":""
    }

    static def nvl(def a, def b) {
        (a?.toString()?.trim().length() > 0)?a:b
    }

    def static toTS(String date) {
        def m = date =~ /(...) (\d\d)\. (...) (\d{4}) (\d{1,2}:\d{2}:\d{2})/
        if (m.find()) {
            def dow = m[0][1]
            def day = m[0][2]
            def mon = m[0][3]
            def year = m[0][4]
            def time = m[0][5]
            switch (mon) {
                case 'led': mon = '01'; break
                case 'úno': mon = '02'; break
                case 'bře': mon = '03'; break
                case 'dub': mon = '04'; break
                case 'kvě': mon = '05'; break
                case 'čer': mon = '06'; break
                case 'črc': mon = '07'; break
                case 'srp': mon = '08'; break
                case 'zář': mon = '09'; break
                case 'říj': mon = '10'; break
                case 'lis': mon = '11'; break
                case 'pro': mon = '12'; break
                default:
                    println("Unknown name $mon")
            }
            return "$year-$mon-${day}T$time"
            //} else {
            //	assert false, "Can not parse '$date'"
        }
    }
}