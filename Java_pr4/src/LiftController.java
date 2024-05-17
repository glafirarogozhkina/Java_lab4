import java.util.*;

// контроллер системы
public class LiftController {
    private List<Lift> lifts; // список лифтов
    private int maxFloors; // количество этажей
    private Queue<LiftRequest> requestsQuery; // очередь ожидания

    // конструктор класса
    public LiftController(int maxFloors, int requestIntervalMin, int requestIntervalMax,
                          int LiftNum, int maxLiftCapacity) {
        // Считываем параметры генерации
        this.maxFloors = maxFloors;

        // Добавляем лифты
        lifts = new ArrayList<>();
        lifts.add(new Lift(1, maxLiftCapacity));
        Random random = new Random();
        for (int i = 1; i < LiftNum; ++i)
            lifts.add(new Lift(random.nextInt(maxFloors - 1) + 1, maxLiftCapacity));

        // Запускаем потоки для каждого лифта
        for (Lift lift : lifts) {
            Thread elevatorThread = new Thread(lift::operate);
            elevatorThread.start();
        }

        requestsQuery = new LinkedList<>();

        // Запускаем поток для генерации новых пассажиров
        Thread generateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(random.nextInt(requestIntervalMax - requestIntervalMin) + requestIntervalMin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                giveRequests();
                generateRequest();
            }
        });
        generateThread.start();
    }

    // Метод для генерации нового пассажира
    private void generateRequest() {
        Random random = new Random();
        int startFloor = random.nextInt(maxFloors) + 1;
        int endFloor = random.nextInt(maxFloors) + 1;
        while (endFloor == startFloor) {
            endFloor = random.nextInt(maxFloors) + 1;
        }

        LiftRequest request = new LiftRequest(startFloor, endFloor);
        findClosestLift(request);
    }

    // Поиск лифта для ожидающих пассажиров
    private void giveRequests() {
        for (int i = 0; i < requestsQuery.size(); ++i) {
            findClosestLift(requestsQuery.poll());
        }
    }

    // Метод поиска подходящего лифта для пассажира
    private void findClosestLift(LiftRequest request) {
        int startFloor = request.getInitialFloor(), endFloor = request.getDestinationFloor();

        // Определяем направление движения пассажира
        String requestDirection = startFloor < endFloor ? "up" : "down";

        // Находим ближайший свободный лифт
        Lift closestLift = null;
        int distance = Integer.MAX_VALUE;
        for (Lift lift : lifts) {
            int liftDistance = Math.abs(lift.getCurrentFloor() - startFloor);

            if (liftDistance < distance && lift.canHold() &&
                    (  lift.getCurrentFloor() == startFloor ||
                            (lift.getState() == LiftState.CLOSEDOORS) ||
                            (lift.getState() == LiftState.UP && requestDirection == "up" && startFloor >= lift.getCurrentFloor()) ||
                            (lift.getState() == LiftState.DOWN && requestDirection == "down" && startFloor <= lift.getCurrentFloor()))
            )
            {
                closestLift = lift;
                distance = liftDistance;
            }
        }

        if (closestLift != null) {
            // Добавляем в список заявок лифта
            closestLift.addRequest(request);
        }
        else {
            // В очередь ожидания
            requestsQuery.add(request);
        }
    }

    public int getFloors() { return maxFloors; }
    public List<Lift> getLifts() { return lifts; }
    public Queue<LiftRequest> getRequestsQuery() {return requestsQuery;}
}