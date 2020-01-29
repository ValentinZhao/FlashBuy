package flashbuy.dao;

import flashbuy.dataobject.SequenceInfoDO;

public interface SequenceInfoDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Thu Jan 23 17:34:43 PST 2020
     */
    int deleteByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Thu Jan 23 17:34:43 PST 2020
     */
    int insert(SequenceInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Thu Jan 23 17:34:43 PST 2020
     */
    int insertSelective(SequenceInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Thu Jan 23 17:34:43 PST 2020
     */
    SequenceInfoDO selectByPrimaryKey(String name);

    SequenceInfoDO getSequenceByName(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Thu Jan 23 17:34:43 PST 2020
     */
    int updateByPrimaryKeySelective(SequenceInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Thu Jan 23 17:34:43 PST 2020
     */
    int updateByPrimaryKey(SequenceInfoDO record);
}