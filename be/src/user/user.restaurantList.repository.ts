import { DataSource, IsNull, Repository, Not } from "typeorm";
import { ConflictException, Injectable } from "@nestjs/common";
import { UserRestaurantListEntity } from "./entities/user.restaurantlist.entity";
import { TokenInfo } from "./user.decorator";
import { SearchInfoDto } from "src/restaurant/dto/seachInfo.dto";
import { ReviewInfoEntity } from "src/review/entities/review.entity";

@Injectable()
export class UserRestaurantListRepository extends Repository<UserRestaurantListEntity> {
    constructor(private dataSource: DataSource) {
        super(UserRestaurantListEntity, dataSource.createEntityManager());
    }
    async addRestaurantToNebob(id: TokenInfo["id"], restaurantId: number, reviewEntity: ReviewInfoEntity) {
        const userRestaurantList = new UserRestaurantListEntity();
        userRestaurantList.userId = id;
        userRestaurantList.restaurantId = restaurantId;
        userRestaurantList.review = reviewEntity;
        userRestaurantList.deletedAt = null;
        userRestaurantList.createdAt = new Date();
        await this.upsert(userRestaurantList, ["userId", "restaurantId"]);
        return null;
    }
    async deleteRestaurantFromNebob(id: TokenInfo["id"], restaurantId: number) {
        await this.update({ userId: id, restaurantId: restaurantId }, { deletedAt: new Date() });
        return null;
    }
    async getTargetRestaurantListInfo(targetId: number) {
        const ids = await this.find({ select: ["restaurantId"], where: { userId: targetId }, order: { createdAt: "DESC" }, take: 3 });
        const restaurantIds = ids.map(entity => entity.restaurantId);
        return await this.createQueryBuilder('user_restaurant_lists')
            .leftJoinAndSelect('user_restaurant_lists.restaurant', 'restaurant')
            .select([
                'user_restaurant_lists.restaurantId',
                'restaurant.name',
                'restaurant.location',
                'restaurant.address',
                'restaurant.category',
                "restaurant.phoneNumber"])
            .where("user_restaurant_lists.restaurantId  IN (:...id)", { id: restaurantIds })
            .getMany();

    }
    async getMyRestaurantListInfo(searchInfoDto: SearchInfoDto, id: TokenInfo["id"]) {
        if (searchInfoDto.radius) {
            return await this
                .createQueryBuilder('user_restaurant_lists')
                .leftJoinAndSelect('user_restaurant_lists.restaurant', 'restaurant')
                .select([
                    'user_restaurant_lists.restaurantId',
                    'restaurant.name',
                    'restaurant.location',
                    'restaurant.address',
                    'restaurant.category',
                    "restaurant.phoneNumber",
                    "restaurant.reviewCnt"
                ])
                .where(`user_restaurant_lists.user_id = :userId and ST_DistanceSphere(
                location, 
                ST_GeomFromText('POINT(${searchInfoDto.longitude} ${searchInfoDto.latitude})', 4326)
            )<  ${searchInfoDto.radius} and user_restaurant_lists.deleted_at IS NULL`, { userId: id })
                .getMany();
        }
        else {
            return await this
                .createQueryBuilder('user_restaurant_lists')
                .leftJoinAndSelect('user_restaurant_lists.restaurant', 'restaurant')
                .select([
                    'user_restaurant_lists.restaurantId AS restaurant_id',
                    'restaurant.name',
                    'restaurant.location',
                    'restaurant.address',
                    'restaurant.category',
                    "restaurant.phoneNumber",
                    "restaurant.reviewCnt"
                ])
                .where('user_restaurant_lists.user_id = :userId  and user_restaurant_lists.deleted_at IS NULL', { userId: id })
                .getRawMany();
        }
    }
}