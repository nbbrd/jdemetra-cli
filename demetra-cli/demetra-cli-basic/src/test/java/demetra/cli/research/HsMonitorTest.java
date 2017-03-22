/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.cli.research;

import org.junit.Test;

/**
 *
 * @author Admin
 */
public class HsMonitorTest {

    public HsMonitorTest() {
    }

    @Test
    public void testSomeMethod() {
        long t0 = System.currentTimeMillis();
        HsMonitor monitor = new HsMonitor();
        monitor.process(data.Data.P);
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(monitor.getResults());
    }

}
