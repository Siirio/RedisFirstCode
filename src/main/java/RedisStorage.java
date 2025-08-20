import org.redisson.Redisson;
import org.redisson.api.RDeque;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;

import java.util.Random;

public class RedisStorage {

    private static final String KEY = "USER_QUEUE";
    private static final int TOTAL_USERS = 20; // количество пользователей
    private static final int PAY_CHANCE = 10;  // 1 из 10 оплачивает

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

        RedissonClient redisson = null;
        try {
            redisson = Redisson.create(config);

            RDeque<String> queue = redisson.getDeque(KEY);
            queue.clear(); // очистим очередь

            // Загружаем пользователей
            for (int i = 1; i <= TOTAL_USERS; i++) {
                queue.add(String.valueOf(i));
            }

            Random random = new Random();

            while (true) {
                // Берём первого пользователя
                String user = queue.pollFirst();
                if (user != null) {
                    System.out.println("— На главной странице показываем пользователя " + user);
                    // возвращаем его в конец очереди
                    queue.addLast(user);
                }

                // 1 из 10 — оплата
                if (random.nextInt(PAY_CHANCE) == 0) {
                    int payingUser = random.nextInt(TOTAL_USERS) + 1;
                    System.out.println("Пользователь " + payingUser + " оплатил услугу.");
                }

                Thread.sleep(1000); // задержка 1 секунда
            }

        } catch (RedisConnectionException e) {
            System.out.println("Не удалось подключиться к Redis: " + e.getMessage());
        } finally {
            if (redisson != null) {
                redisson.shutdown();
            }
        }
    }
}