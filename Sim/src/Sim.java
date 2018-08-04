import java.util.*;
class Sim {

    // Class Sim variables
    public static double Clock, AvgInterArrivalTime, AvgServiceTime, StanDeviation, 
            LastEventTime, TotalBusy, MaximumQueueLength, SumResponseTime;
    public static long  NumberOfCustomers, QueueLength, NumberInService,
            TotalCustomers, NumberOfDepartures, LongService;

    public final static int Arrival = 1;
    public final static int Departure = 2;

    public static EventList FutureEventList;
    public static Queue Customers;
    public static Random Stream;

    public static void main(String argv[]) {

        AvgInterArrivalTime = 4.5; AvgServiceTime = 3.2;
        StanDeviation                = 0.6; TotalCustomers  = 1000;
        long seed            = Long.parseLong("2");

        Stream = new Random(seed);           // initialize rng Stream
        FutureEventList = new EventList();
        Customers = new Queue();

        Initialization();

        // Loop until first "TotalCustomers" have departed
        while(NumberOfDepartures < TotalCustomers ) {
            Event evt = (Event)FutureEventList.getMin();  // get forthcoming event
            FutureEventList.dequeue();                    // release it
            Clock = evt.get_time();                       // advance simulation time
            if( evt.get_type() == Arrival ) ProcessArrival(evt);
            else  ProcessDeparture(evt);
        }
        ReportGeneration();   // Generate Final Report
     }

     // seed the event list with TotalCustomers arrivals
     public static void Initialization()   { 
        Clock = 0.0;
        QueueLength = 0;
        NumberInService = 0;
        LastEventTime = 0.0;
        TotalBusy = 0;
        MaximumQueueLength = 0;
        SumResponseTime = 0;
        NumberOfDepartures = 0;
        LongService = 0;

        // create first Arrival event
        Event evt = new Event(Arrival, exponential(Stream, AvgInterArrivalTime));
        FutureEventList.enqueue( evt );
     }

     public static void ProcessArrival(Event evt) {
        Customers.enqueue(evt); 
        QueueLength++;

        // if the server is idle, fetch the event, do statistics
        // and put into service

        if( NumberInService == 0) ScheduleDeparture();
        else TotalBusy += (Clock - LastEventTime);  // server is busy

        // adjust max queue length statistics
        if (MaximumQueueLength < QueueLength) MaximumQueueLength = QueueLength;

        // schedule the next Arrival
        Event next_arrival = new Event(Arrival, Clock+exponential(Stream, AvgInterArrivalTime));
        FutureEventList.enqueue( next_arrival );
        LastEventTime = Clock;
     }

     public static void ScheduleDeparture() {
        double ServiceTime;

        // get the job at the head of the queue
        while (( ServiceTime = normal(Stream, AvgServiceTime, StanDeviation)) < 0 );
        Event depart = new Event(Departure,Clock+ServiceTime);
        FutureEventList.enqueue( depart );
        NumberInService = 1;
        QueueLength--;
     }

    public static void ProcessDeparture(Event e) {

        // get the customer description
        Event finished = (Event) Customers.dequeue();

        // if there are customers in the queue then schedule
        // the Departure of the next one
         if( QueueLength > 0 ) ScheduleDeparture();
         else NumberInService = 0;

         // measure the response time and add to the sum
         double response = (Clock - finished.get_time());
         SumResponseTime += response;
         
         if( response > 4.0 ) LongService++; // record long service
         TotalBusy += (Clock - LastEventTime );
         NumberOfDepartures++;
         LastEventTime = Clock;                            
     }

    public static void ReportGeneration() {
        double RHO   = TotalBusy/Clock;
        double AVGR  = SumResponseTime/TotalCustomers;
        double PC4   = ((double)LongService)/TotalCustomers;


        System.out.println("SINGLE SERVER QUEUE SIMULATION - GROCERY STORE CHECKOUT COUNTER");
        System.out.println();
        System.out.println("\tMEAN INTERARRIVAL TIME                         " + AvgInterArrivalTime);
        System.out.println("\tMEAN SERVICE TIME                              " + AvgServiceTime);
        System.out.println("\tSTANDARD DEVIATION OF SERVICE TIMES            " + StanDeviation);
        System.out.println("\tNUMBER OF CUSTOMERS SERVED                    " + TotalCustomers);
        System.out.println(); 
        System.out.println("\tSERVER UTILIZATION                            " + RHO );
        System.out.println("\tCUSTOMERS MAXIMUM QUEUE LINE LENGTH           " + MaximumQueueLength);
        System.out.println("\tAVERAGE RESPONSE TIME                         " + AVGR + "  MINUTES");
        System.out.println("\tPROPORTION WHO SPEND FOUR "); 
        System.out.println("\t MINUTES OR MORE IN SYSTEM                    " + PC4);
        System.out.println("\tSIMULATION RUNTIME LENGTH                     " + Clock + " MINUTES");
        System.out.println("\tNUMBER OF DEPARTURES                          " + TotalCustomers);
    }

    // Generate samples from exponential distribution with mean
    public static double exponential(Random rng, double mean) {
        return -mean*Math.log( rng.nextDouble() );
    }

    public static double SaveNormal;
    public static int  NumNormals = 0;
    public static final double  PI = 3.1415927 ;

    //Generate samples from normal distribution with mean and standard deviation
    public static double normal(Random rng, double mean, double sigma) {
        double ReturnNormal;

        // should we generate two normals?
        if(NumNormals == 0 ) {
            double r1 = rng.nextDouble();
            double r2 = rng.nextDouble();
            ReturnNormal = Math.sqrt(-2*Math.log(r1))*Math.cos(2*PI*r2);
            SaveNormal   = Math.sqrt(-2*Math.log(r1))*Math.sin(2*PI*r2);
            NumNormals = 1;
        } else {
            NumNormals = 0;
            ReturnNormal = SaveNormal;
        }
        return ReturnNormal*sigma + mean ;
    }
}

