/*
 * $RCSfile: PackageUtil.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.3 $
 * $Date: 2006-03-31 19:43:38 $
 * $State: Exp $
 */
package com.tomgibara.imageio.impl.common;

//import com.sun.medialib.codec.jiio.Util;

public class PackageUtil {

    /**
     * Implementation version derived from Manifest.
     */
    private static String version = "1.0";

    /**
     * Implementation vendor derived from Manifest.
     */
    private static String vendor = "Sun Microsystems, Inc.";

    /**
     * Specification Title derived from Manifest.
     */
    private static String specTitle = "Java Advanced Imaging Image I/O Tools";

    /**
     * Set static flags.
     */
    static {
        // Set version and vendor strings.
        try {
            Class thisClass =
                Class.forName("com.tomgibara.imageio.impl.common.PackageUtil");
            Package thisPackage = thisClass.getPackage();
            String version = thisPackage.getImplementationVersion();
            if (version != null) PackageUtil.version = version;
            String vendor = thisPackage.getImplementationVendor();
            if (vendor != null) PackageUtil.vendor = vendor;
            String specTitle = thisPackage.getSpecificationTitle();
            if (specTitle != null) specTitle = specTitle;
        } catch(ClassNotFoundException e) {
        }
    }

    /**
     * Return a version string for the package.
     */
    public static final String getVersion() {
        return version;
    }

    /**
     * Return a vendor string for the package.
     */
    public static final String getVendor() {
        return vendor;
    }

    /**
     * Return the Specification Title string for the package.
     */
    public static final String getSpecificationTitle() {
        return specTitle;
    }
}
