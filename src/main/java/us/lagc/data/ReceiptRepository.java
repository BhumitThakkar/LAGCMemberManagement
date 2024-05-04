package us.lagc.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import us.lagc.model.Receipt;

@Repository
public interface ReceiptRepository extends CrudRepository<Receipt, Integer> {

}
