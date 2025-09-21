public class Main
{
    public static double calculateVolume (double d){
        double r = d/2;
        return (4.0/3.0) * Math.PI * Math.pow(r,3);
    }

    //main
    public static void main(String[] args)
    {
        double d_Sun = 865000.0;
        double d_earth = 7600.0;

        double v_earth= calculateVolume(d_earth);
        double V_sun= calculateVolume(d_Sun);
        double ratio = V_sun/v_earth;
        System.out.printf("The volume of the earth is %.2f cubic miles.\nThe volume of the sun is %.2f cubic miles.", v_earth,V_sun);
        System.out.printf("\nThe ratio of volume is Sun:Earth = 1:%.2f", ratio);
    }
}