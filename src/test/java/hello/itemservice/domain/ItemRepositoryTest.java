package hello.itemservice.domain;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCondition;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.memory.MemoryItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class ItemRepositoryTest {

    /**
     * 구현체를 테스트 하는것이 아니라 인터페이스를 테스트 하고 있다
     * 장점은 향후에 구현체를 수정하게 됐을 때 해당 구현체가 잘 동작하는지 같은 테스트로 편리하게 할 수 있다
     */

    /**
     * @Transactional의 원리
     * 스프링이 제공하는 @Transactional 어노테이션은 로직이 성공적으로 수행되면 커밋되도록 동작한다
     * 그런데 @Transactional 어노테이션을 테스트에서 사용하면 아주 특별하게 동작한다
     * @Transactional이 테스트에 있으면 스프링은 테스트를 트랜잭션 안에서 실행하고, 테스트가 끝나면 트랜잭션을 자동으로 롤백시켜 버린다
     */

    @Autowired
    ItemRepository itemRepository;

    /**
     * AfterEach :
     * 테스트 구동 시 다른 테스트에 영향을 주지 않도록 clearStore()를 통해 다음 테스트에 영향을 주지 않기 하기 위함.
     */
    @AfterEach
    void afterEach() {
        //MemoryItemRepository 의 경우 제한적으로 사용
        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }
    }

    /**
     * @Commit
     * @Rollback(value = false)
     * 그래도 첫 테스트시에는 DB에 데이터가 진짜 들어가는지 확인을 해야 할때가 있다
     * 그럴때는 @Transactional 범위 안에서 @Commit, @Rollback(value = false) 를 하면
     * 테스트에서 트랜잭션이 끝날 때 자동 롤백되는것을 방지할 수 있다
     */
    @Test
//    @Commit
//    @Rollback(value = false)
//    @Transactional
    void save() {
        //given
        Item item = new Item("itemA", 10000, 10);

        //when
        Item savedItem = itemRepository.save(item);

        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void updateItem() {
        //given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        //then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findItems() {
        //given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        //둘 다 없음 검증
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        //itemName 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice 검증
        test(null, 10000, item1);

        //둘 다 있음 검증
        test("itemA", 10000, item1);
    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCondition(itemName, maxPrice));
        assertThat(result).containsExactly(items); // containsExactly 는 순서까지 맞아야 됨
    }
}
